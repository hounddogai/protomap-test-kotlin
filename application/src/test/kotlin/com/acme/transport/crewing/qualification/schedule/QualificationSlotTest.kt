package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationSlotTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationSlot("code-947", "title-118")
        assertEquals(QualificationSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newQualificationSlot("code-947", "title-118")
        try {
            item.moveTo(QualificationSlotStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationSlotStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationSlot("code-947", "title-118")
        item.status = QualificationSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationSlotStatus.OPEN) }
    }
}
