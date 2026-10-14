package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newIdlingPolicy("code-907", "title-439")
        assertEquals(IdlingPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newIdlingPolicy("code-907", "title-439")
        try {
            item.moveTo(IdlingPolicyStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingPolicyStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIdlingPolicy("code-907", "title-439")
        item.status = IdlingPolicyStatus.FAILED
        assertFails("move") { item.moveTo(IdlingPolicyStatus.NEW) }
    }
}
