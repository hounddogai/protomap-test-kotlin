package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalInspectionWindow("code-995", "title-717")
        assertEquals(GlobalInspectionWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalInspectionWindow("code-995", "title-717")
        try {
            item.moveTo(GlobalInspectionWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalInspectionWindow("code-995", "title-717")
        item.status = GlobalInspectionWindowStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalInspectionWindowStatus.DRAFT) }
    }
}
