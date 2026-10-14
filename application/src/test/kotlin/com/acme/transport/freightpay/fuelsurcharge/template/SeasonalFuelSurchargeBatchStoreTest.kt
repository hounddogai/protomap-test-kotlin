package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalFuelSurchargeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalFuelSurchargeBatchStore()
        val item = newSeasonalFuelSurchargeBatch("code-933", "name-84")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
