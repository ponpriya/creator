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
        name = "created_by",
        nullable=true,
        updatable=false
    )
    protected String createdBy;

    @CreatedDate
    @Column(
        name = "created_date",
        nullable=false,
        updatable=false
    )
    protected LocalDateTime createdDate;

    @LastModifiedBy
    @Column(
        name = "last_modified_by",
        insertable=false
    )
    protected String lastModifiedBy;

    @LastModifiedDate
    @Column(
        name = "last_modified_date",
        insertable=false
    )
    protected LocalDateTime lastModifiedDate;

}
