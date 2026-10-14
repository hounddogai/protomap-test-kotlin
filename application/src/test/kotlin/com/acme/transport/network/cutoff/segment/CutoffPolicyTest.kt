package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newCutoffPolicy("code-86", "title-456")
        assertEquals(CutoffPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCutoffPolicy("code-86", "title-456")
        try {
            item.moveTo(CutoffPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCutoffPolicy("code-86", "title-456")
        item.status = CutoffPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(CutoffPolicyStatus.CREATED) }
    }
}
