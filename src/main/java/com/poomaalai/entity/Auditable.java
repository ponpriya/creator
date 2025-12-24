package com.poomaalai.entity;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedBy
    @Column(
        nullable=false,
        updatable=false
    )
    protected String createdBy;

    @CreatedDate
    @Column(
        nullable=false,
        updatable=false
    )
    protected LocalDateTime createdDate;

    @LastModifiedBy
    @Column(insertable=false)
    protected String lastModifiedBy;

    @LastModifiedDate
    @Column(insertable=false)
    protected LocalDateTime lastModifiedDate;

}
