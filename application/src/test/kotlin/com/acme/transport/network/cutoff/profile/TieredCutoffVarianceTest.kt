package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCutoffVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredCutoffVariance("code-889", "title-777")
        assertEquals(TieredCutoffVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredCutoffVariance("code-889", "title-777")
        try {
            item.moveTo(TieredCutoffVarianceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCutoffVarianceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredCutoffVariance("code-889", "title-777")
        item.status = TieredCutoffVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredCutoffVarianceStatus.REQUESTED) }
    }
}
