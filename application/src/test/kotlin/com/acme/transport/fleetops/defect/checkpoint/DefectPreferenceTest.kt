package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectPreferenceTest {
    @Test
    fun `starts in draft`() {
        val item = newDefectPreference("code-917", "title-321")
        assertEquals(DefectPreferenceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefectPreference("code-917", "title-321")
        try {
            item.moveTo(DefectPreferenceStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectPreferenceStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefectPreference("code-917", "title-321")
        item.status = DefectPreferenceStatus.CLOSED
        assertFails("move") { item.moveTo(DefectPreferenceStatus.DRAFT) }
    }
}
