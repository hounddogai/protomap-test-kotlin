package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireLedgerEngine(newRegistry())
        val item = newTireLedger("code-979", "name-920")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
