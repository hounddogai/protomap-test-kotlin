package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAccessorialAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAccessorialAllocationStore()
        val item = newSeasonalAccessorialAllocation("code-860", "label-217")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
