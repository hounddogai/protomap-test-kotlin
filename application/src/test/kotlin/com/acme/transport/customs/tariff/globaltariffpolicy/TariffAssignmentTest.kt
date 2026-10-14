package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newTariffAssignment("code-994", "name-510")
        assertEquals(TariffAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTariffAssignment("code-994", "name-510")
        try {
            item.moveTo(TariffAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTariffAssignment("code-994", "name-510")
        item.status = TariffAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(TariffAssignmentStatus.IDLE) }
    }
}
