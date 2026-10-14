package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialVariance("code-460", "label-350")
        assertEquals(AccessorialVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAccessorialVariance("code-460", "label-350")
        try {
            item.moveTo(AccessorialVarianceStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialVarianceStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialVariance("code-460", "label-350")
        item.status = AccessorialVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialVarianceStatus.OPEN) }
    }
}
