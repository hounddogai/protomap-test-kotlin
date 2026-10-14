package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRouteOffsetThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRouteOffsetThresholdStore()
        val item = newStrictRouteOffsetThreshold("code-22", "title-710")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
