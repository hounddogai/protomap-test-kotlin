package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRateCardManifestTest {
    @Test
    fun `starts in new`() {
        val item = newLenientRateCardManifest("code-200", "label-750")
        assertEquals(LenientRateCardManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLenientRateCardManifest("code-200", "label-750")
        try {
            item.moveTo(LenientRateCardManifestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRateCardManifestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientRateCardManifest("code-200", "label-750")
        item.status = LenientRateCardManifestStatus.FAILED
        assertFails("move") { item.moveTo(LenientRateCardManifestStatus.NEW) }
    }
}
