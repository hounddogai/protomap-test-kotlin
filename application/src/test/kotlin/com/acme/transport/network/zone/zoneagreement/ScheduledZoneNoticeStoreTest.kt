package com.acme.transport.network.zone.zoneagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledZoneNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledZoneNoticeStore()
        val item = newScheduledZoneNotice("code-442", "name-884")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
