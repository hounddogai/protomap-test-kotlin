package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearancePolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newClearancePolicy("code-635", "name-638")
        assertEquals(ClearancePolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newClearancePolicy("code-635", "name-638")
        try {
            item.moveTo(ClearancePolicyStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearancePolicyStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newClearancePolicy("code-635", "name-638")
        item.status = ClearancePolicyStatus.STOPPED
        assertFails("move") { item.moveTo(ClearancePolicyStatus.IDLE) }
    }
}
