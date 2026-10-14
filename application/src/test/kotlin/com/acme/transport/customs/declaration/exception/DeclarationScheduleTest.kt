package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationSchedule("code-507", "name-392")
        assertEquals(DeclarationScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDeclarationSchedule("code-507", "name-392")
        try {
            item.moveTo(DeclarationScheduleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationScheduleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationSchedule("code-507", "name-392")
        item.status = DeclarationScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationScheduleStatus.IDLE) }
    }
}
