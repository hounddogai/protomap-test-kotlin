package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newLockerTemplate("code-541", "name-825")
        assertEquals(LockerTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLockerTemplate("code-541", "name-825")
        try {
            item.moveTo(LockerTemplateStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerTemplateStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLockerTemplate("code-541", "name-825")
        item.status = LockerTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(LockerTemplateStatus.PLANNED) }
    }
}
