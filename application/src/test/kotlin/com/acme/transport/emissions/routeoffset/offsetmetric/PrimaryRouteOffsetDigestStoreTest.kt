package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRouteOffsetDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRouteOffsetDigestStore()
        val item = newPrimaryRouteOffsetDigest("code-440", "name-29")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
