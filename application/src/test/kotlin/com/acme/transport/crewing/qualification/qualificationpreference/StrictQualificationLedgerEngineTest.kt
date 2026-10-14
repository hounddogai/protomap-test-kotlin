package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictQualificationLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictQualificationLedgerEngine()
        val item = newStrictQualificationLedger("code-464", "name-34")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
