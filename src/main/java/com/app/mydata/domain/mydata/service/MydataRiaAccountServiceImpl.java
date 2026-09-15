package com.app.mydata.domain.mydata.service;

import com.app.mydata.domain.mydata.dto.MydataRiaAccountDTO;
import com.app.mydata.domain.mydata.dto.request.MydataRiaAccountRequestDTO;
import com.app.mydata.domain.mydata.dto.request.RiaAccountRequestDTO;
import com.app.mydata.domain.mydata.dto.response.MydataRiaAccountResponseDTO;
import com.app.mydata.domain.mydata.exception.MydataRiaAccountException;
import com.app.mydata.domain.mydata.exception.MydataRiaAccountNotFoundException;
import com.app.mydata.domain.mydata.mapper.MydataKeyMapper;
import com.app.mydata.domain.mydata.mapper.MydataRiaAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MydataRiaAccountServiceImpl implements MydataRiaAccountService {

    private final MydataRiaAccountMapper mydataRiaAccountMapper;
    private final MydataKeyMapper mydataKeyMapper;

    @Override
    public List<MydataRiaAccountResponseDTO> getAccountsByCiHash(
            MydataRiaAccountRequestDTO request) {

        String ciHash = request.getCiHash();

        if (mydataKeyMapper.existsByCiHash(ciHash) == 0) {
            throw new MydataRiaAccountNotFoundException("등록되지 않은 CiHash입니다.");
        }

        List<MydataRiaAccountDTO> accounts = mydataRiaAccountMapper.selectByCiHash(ciHash);

        return accounts.stream()
                .map(MydataRiaAccountResponseDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    public MydataRiaAccountResponseDTO syncRiaAccount(RiaAccountRequestDTO riaAccountRequestDTO) {
        MydataRiaAccountDTO riaAccountDTO = riaAccountRequestDTO.toDTO();
        if (mydataKeyMapper.existsByCiHash(riaAccountDTO.getCiHash()) == 0) {
            throw new MydataRiaAccountNotFoundException("등록되지 않은 사용자 입니다.");
        }

        mydataRiaAccountMapper.upsertAccount(riaAccountDTO);
        MydataRiaAccountDTO savedAccount = mydataRiaAccountMapper.selectByCiHashAndBrokerName(riaAccountDTO)
                .orElseThrow(() -> new MydataRiaAccountException("재조회 실패"));
        return MydataRiaAccountResponseDTO.of(savedAccount);
    }

}
