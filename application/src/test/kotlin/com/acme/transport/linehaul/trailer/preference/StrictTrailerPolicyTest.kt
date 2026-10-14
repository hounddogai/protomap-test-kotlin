package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newStrictTrailerPolicy("code-517", "title-484")
        assertEquals(StrictTrailerPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newStrictTrailerPolicy("code-517", "title-484")
        try {
            item.moveTo(StrictTrailerPolicyStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerPolicyStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictTrailerPolicy("code-517", "title-484")
        item.status = StrictTrailerPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(StrictTrailerPolicyStatus.CREATED) }
    }
}
