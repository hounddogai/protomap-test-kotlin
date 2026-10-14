package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAccessorialBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newManualAccessorialBaseline("code-767", "label-211")
        assertEquals(ManualAccessorialBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualAccessorialBaseline("code-767", "label-211")
        try {
            item.moveTo(ManualAccessorialBaselineStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAccessorialBaselineStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualAccessorialBaseline("code-767", "label-211")
        item.status = ManualAccessorialBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(ManualAccessorialBaselineStatus.CREATED) }
    }
}
