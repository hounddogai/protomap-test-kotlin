package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRouteOffsetExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRouteOffsetExceptionStore()
        val item = newLenientRouteOffsetException("code-944", "label-519")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
