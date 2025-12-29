package sf.mifi.grechko.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import sf.mifi.grechko.dto.EnrollmentDto;
import sf.mifi.grechko.models.Enrollment;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    EnrollmentMapper INSTANCE = Mappers.getMapper(EnrollmentMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    EnrollmentDto toDTO(Enrollment enrollment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Enrollment toEntity(EnrollmentDto.CreateRequest request);
}
