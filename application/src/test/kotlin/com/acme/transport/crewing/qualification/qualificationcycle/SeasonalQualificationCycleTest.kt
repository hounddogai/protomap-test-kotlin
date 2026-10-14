package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalQualificationCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalQualificationCycle("code-632", "title-613")
        assertEquals(SeasonalQualificationCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalQualificationCycle("code-632", "title-613")
        try {
            item.moveTo(SeasonalQualificationCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalQualificationCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalQualificationCycle("code-632", "title-613")
        item.status = SeasonalQualificationCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalQualificationCycleStatus.PROPOSED) }
    }
}
