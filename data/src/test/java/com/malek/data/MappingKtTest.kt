package com.malek.data

import com.google.common.truth.Truth
import com.malek.data.api.dto.FieldsDto
import com.malek.data.api.dto.RecordDto
import com.malek.data.api.dto.toPublicToilet
import com.malek.domain.models.PublicToilet
import com.malek.domain.models.Service
import org.junit.Test

class MappingKtTest {

    @Test
    fun should_drop_dto_when_no_id() {
        // Given
        val fieldsDto = FieldsDto(
            adresse = "test address",
            gestionnaire = null,
            source = null,
            complementAdresse = "test",
            babyRely = "Yes",
            equipmentInfoUrl = null,
            prmAccess = "Yes",
            geoPoint2d = null,
            arrondissement = 75010,
            horaire = "24/24"
        )
        val recordDto = RecordDto(
            recordId = null,
            fields = fieldsDto,
        )
        // then
        Truth.assertThat(recordDto.toPublicToilet()).isNull()
    }

    @Test
    fun should_drop_dto_when_no_address() {
        // Given
        val fieldsDto = FieldsDto(
            adresse = null,
            gestionnaire = null,
            source = null,
            complementAdresse = "test",
            babyRely = "Yes",
            equipmentInfoUrl = null,
            prmAccess = "Yes",
            geoPoint2d = null,
            arrondissement = 75010,
            horaire = "24/24"
        )
        // then
        val recordDto = RecordDto(
            recordId = "id",
            fields = fieldsDto,
        )
        Truth.assertThat(recordDto.toPublicToilet()).isNull()
    }

    @Test
    fun should_map_dto_when_rigth_infos_to_domain() {
        // Given
        val fieldsDto = FieldsDto(
            adresse = "test address",
            gestionnaire = null,
            source = null,
            complementAdresse = "complementAdresse",
            babyRely = "Oui",
            equipmentInfoUrl = "url",
            prmAccess = "Oui",
            geoPoint2d = listOf(42.1, 8.1),
            arrondissement = 75010,
            horaire = "24/24"
        )
        val recordDto = RecordDto(
            recordId = "id",
            fields = fieldsDto,
        )
        // then
        Truth.assertThat(recordDto.toPublicToilet()).isEqualTo(
            PublicToilet(
                address = "test address, 75010",
                servicesAvailable = listOf(
                    Service.PRM_ACCESS, Service.BABY_RELY
                ),
                id = "id",
                equipmentInfoUrl = "url",
                latLong = 42.1 to 8.1,
                hours = "24/24"
            )
        )
    }

    @Test
    fun should_drop_arrondissement_when_its_null() {
        // Given
        val fieldsDto = FieldsDto(
            adresse = "test address",
            gestionnaire = null,
            source = null,
            complementAdresse = "",
            babyRely = "Oui",
            equipmentInfoUrl = "url",
            prmAccess = "Oui",
            geoPoint2d = listOf(42.1, 8.1),
            arrondissement = null,
            horaire = "24/24"
        )
        val recordDto = RecordDto(
            recordId = "id",
            fields = fieldsDto,
        )
        // then
        Truth.assertThat(recordDto.toPublicToilet()).isEqualTo(
            PublicToilet(
                address = "test address",
                servicesAvailable = listOf(
                    Service.PRM_ACCESS, Service.BABY_RELY
                ),
                id = "id",
                equipmentInfoUrl = "url",
                latLong = 42.1 to 8.1,
                hours = "24/24"
            )
        )
    }

    @Test
    fun should_drop_latlong_when_geoPoint2d_is_corrupted() {
        // Given
        val fieldsDto = FieldsDto(
            adresse = "test address",
            gestionnaire = null,
            source = null,
            complementAdresse = "complementAdresse",
            babyRely = "Oui",
            equipmentInfoUrl = "url",
            prmAccess = "Oui",
            geoPoint2d = listOf(42.1),
            arrondissement = 75010,
            horaire = "24/24"
        )
        val recordDto = RecordDto(
            recordId = "id",
            fields = fieldsDto,
        )
        // then
        Truth.assertThat(recordDto.toPublicToilet()).isEqualTo(
            PublicToilet(
                address = "test address, 75010",
                servicesAvailable = listOf(
                    Service.PRM_ACCESS, Service.BABY_RELY
                ),
                id = "id",
                equipmentInfoUrl = "url",
                latLong = null,
                hours = "24/24"
            )
        )
    }

}