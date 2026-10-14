package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientParcelPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientParcelPolicyStore()
        val item = newLenientParcelPolicy("code-460", "name-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
