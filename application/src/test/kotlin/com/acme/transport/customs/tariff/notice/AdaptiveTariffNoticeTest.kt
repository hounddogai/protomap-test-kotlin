package com.acme.transport.customs.tariff.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTariffNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveTariffNotice("code-519", "name-918")
        assertEquals(AdaptiveTariffNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveTariffNotice("code-519", "name-918")
        try {
            item.moveTo(AdaptiveTariffNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTariffNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveTariffNotice("code-519", "name-918")
        item.status = AdaptiveTariffNoticeStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveTariffNoticeStatus.NEW) }
    }
}
