package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelBurnDigestTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalFuelBurnDigest("code-402", "label-596")
        assertEquals(GlobalFuelBurnDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newGlobalFuelBurnDigest("code-402", "label-596")
        try {
            item.moveTo(GlobalFuelBurnDigestStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelBurnDigestStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalFuelBurnDigest("code-402", "label-596")
        item.status = GlobalFuelBurnDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalFuelBurnDigestStatus.OPEN) }
    }
}
