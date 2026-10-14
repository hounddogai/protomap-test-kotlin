package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalQualificationMappingTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalQualificationMapping("code-407", "title-445")
        assertEquals(SeasonalQualificationMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalQualificationMapping("code-407", "title-445")
        try {
            item.moveTo(SeasonalQualificationMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalQualificationMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalQualificationMapping("code-407", "title-445")
        item.status = SeasonalQualificationMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalQualificationMappingStatus.OPEN) }
    }
}
