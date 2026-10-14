package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultBackhaulVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultBackhaulVariance("code-764", "name-3")
        assertEquals(DefaultBackhaulVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultBackhaulVariance("code-764", "name-3")
        try {
            item.moveTo(DefaultBackhaulVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultBackhaulVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultBackhaulVariance("code-764", "name-3")
        item.status = DefaultBackhaulVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultBackhaulVarianceStatus.PROPOSED) }
    }
}
