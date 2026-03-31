package io.soyoung.addashboard.controller;

import io.soyoung.addashboard.dto.LeadDto;
import io.soyoung.addashboard.dto.LeadListResponse;
import io.soyoung.addashboard.entity.Lead;
import io.soyoung.addashboard.entity.LeadStatus;
import io.soyoung.addashboard.repository.LeadRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 리드 관리 API 컨트롤러.
 * 상담 가능 여부 등 유효 리드 필터링 조회를 제공한다.
 */
@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadRepository leadRepository;

    /**
     * 리드 목록을 조회한다.
     * status 또는 metaCampaignId 파라미터로 필터링할 수 있다.
     *
     * @param status         리드 상태 (NEW, VERIFIED, REJECTED) (선택)
     * @param metaCampaignId Meta 캠페인 ID (선택)
     * @return 리드 목록 응답
     */
    @GetMapping
    public ResponseEntity<LeadListResponse> getLeads(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String metaCampaignId) {
        List<Lead> leads;

        if (status != null && !status.isEmpty()) {
            try {
                leads = leadRepository.findAllByStatus(LeadStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 리드 상태입니다: " + status);
            }
        } else if (metaCampaignId != null && !metaCampaignId.isEmpty()) {
            leads = leadRepository.findAllByMetaCampaignId(metaCampaignId);
        } else {
            leads = leadRepository.findAll();
        }

        List<LeadDto> leadDtos = leads.stream()
            .map(lead -> LeadDto.builder()
                .leadId(lead.getId())
                .customerName(lead.getName())
                .email(lead.getEmail())
                .status(lead.getStatus().name())
                .isQualified(lead.getStatus() == LeadStatus.VERIFIED)
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(LeadListResponse.builder().leads(leadDtos).build());
    }
}
