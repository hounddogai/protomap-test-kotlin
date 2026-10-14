package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetPolicyStore()
        val item = newRouteOffsetPolicy("code-486", "name-91")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
