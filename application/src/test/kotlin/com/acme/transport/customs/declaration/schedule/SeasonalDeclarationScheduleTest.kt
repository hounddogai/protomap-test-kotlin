package com.acme.transport.customs.declaration.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalDeclarationSchedule("code-293", "label-624")
        assertEquals(SeasonalDeclarationScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalDeclarationSchedule("code-293", "label-624")
        try {
            item.moveTo(SeasonalDeclarationScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalDeclarationSchedule("code-293", "label-624")
        item.status = SeasonalDeclarationScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalDeclarationScheduleStatus.REQUESTED) }
    }
}
