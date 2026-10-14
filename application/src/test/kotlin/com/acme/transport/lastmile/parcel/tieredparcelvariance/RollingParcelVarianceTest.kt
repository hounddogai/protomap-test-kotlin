package com.acme.transport.lastmile.parcel.tieredparcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingParcelVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingParcelVariance("code-575", "label-975")
        assertEquals(RollingParcelVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRollingParcelVariance("code-575", "label-975")
        try {
            item.moveTo(RollingParcelVarianceStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingParcelVarianceStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingParcelVariance("code-575", "label-975")
        item.status = RollingParcelVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(RollingParcelVarianceStatus.DRAFT) }
    }
}
