package sf.mifi.grechko.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sf.mifi.grechko.dto.AssignmentDto;
import sf.mifi.grechko.dto.LessonDto;
import sf.mifi.grechko.dto.SubmissionDto;
import sf.mifi.grechko.repositories.AssignmentRepository;
import sf.mifi.grechko.repositories.SubmissionRepository;
import sf.mifi.grechko.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public List<SubmissionDto> getAll() {
        return submissionRepository.findAllWithDetails()
                .stream()
                .map(SubmissionDto::fromEntity)
                .toList();
    }

}
