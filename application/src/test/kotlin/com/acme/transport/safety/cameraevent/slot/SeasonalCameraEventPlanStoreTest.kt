package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCameraEventPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCameraEventPlanStore()
        val item = newSeasonalCameraEventPlan("code-214", "title-873")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
