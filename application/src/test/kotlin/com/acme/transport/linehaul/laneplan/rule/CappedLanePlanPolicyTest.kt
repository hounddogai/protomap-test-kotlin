package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLanePlanPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedLanePlanPolicy("code-707", "name-921")
        assertEquals(CappedLanePlanPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedLanePlanPolicy("code-707", "name-921")
        try {
            item.moveTo(CappedLanePlanPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLanePlanPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedLanePlanPolicy("code-707", "name-921")
        item.status = CappedLanePlanPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedLanePlanPolicyStatus.REQUESTED) }
    }
}
