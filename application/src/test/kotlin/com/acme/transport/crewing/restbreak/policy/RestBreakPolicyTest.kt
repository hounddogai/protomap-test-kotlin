package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newRestBreakPolicy("code-552", "title-823")
        assertEquals(RestBreakPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRestBreakPolicy("code-552", "title-823")
        try {
            item.moveTo(RestBreakPolicyStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakPolicyStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRestBreakPolicy("code-552", "title-823")
        item.status = RestBreakPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(RestBreakPolicyStatus.PLANNED) }
    }
}
