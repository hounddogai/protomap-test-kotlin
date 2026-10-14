package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIdlingVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultIdlingVariance("code-717", "name-511")
        assertEquals(DefaultIdlingVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefaultIdlingVariance("code-717", "name-511")
        try {
            item.moveTo(DefaultIdlingVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIdlingVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultIdlingVariance("code-717", "name-511")
        item.status = DefaultIdlingVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultIdlingVarianceStatus.PROPOSED) }
    }
}
