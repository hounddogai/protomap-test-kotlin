package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBackhaulClaimTest {
    @Test
    fun `starts in new`() {
        val item = newManualBackhaulClaim("code-312", "label-465")
        assertEquals(ManualBackhaulClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualBackhaulClaim("code-312", "label-465")
        try {
            item.moveTo(ManualBackhaulClaimStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBackhaulClaimStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualBackhaulClaim("code-312", "label-465")
        item.status = ManualBackhaulClaimStatus.FAILED
        assertFails("move") { item.moveTo(ManualBackhaulClaimStatus.NEW) }
    }
}
