package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackFuelSurchargeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackFuelSurchargeMilestoneStore()
        val item = newFallbackFuelSurchargeMilestone("code-764", "title-52")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
