package com.acme.transport.terminal.dwell.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newDwellPolicy("code-858", "title-360")
        assertEquals(DwellPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDwellPolicy("code-858", "title-360")
        try {
            item.moveTo(DwellPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDwellPolicy("code-858", "title-360")
        item.status = DwellPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(DwellPolicyStatus.CREATED) }
    }
}
