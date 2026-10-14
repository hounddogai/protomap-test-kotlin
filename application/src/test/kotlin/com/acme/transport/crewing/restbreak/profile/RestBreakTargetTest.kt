package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakTargetTest {
    @Test
    fun `starts in created`() {
        val item = newRestBreakTarget("code-709", "label-377")
        assertEquals(RestBreakTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRestBreakTarget("code-709", "label-377")
        try {
            item.moveTo(RestBreakTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRestBreakTarget("code-709", "label-377")
        item.status = RestBreakTargetStatus.RETIRED
        assertFails("move") { item.moveTo(RestBreakTargetStatus.CREATED) }
    }
}
