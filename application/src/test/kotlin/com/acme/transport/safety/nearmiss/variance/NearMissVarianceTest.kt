package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newNearMissVariance("code-341", "title-853")
        assertEquals(NearMissVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newNearMissVariance("code-341", "title-853")
        try {
            item.moveTo(NearMissVarianceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissVarianceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNearMissVariance("code-341", "title-853")
        item.status = NearMissVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(NearMissVarianceStatus.CREATED) }
    }
}
