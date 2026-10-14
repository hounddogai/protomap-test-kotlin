package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCameraEventQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCameraEventQuotaStore()
        val item = newSeasonalCameraEventQuota("code-310", "name-721")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
