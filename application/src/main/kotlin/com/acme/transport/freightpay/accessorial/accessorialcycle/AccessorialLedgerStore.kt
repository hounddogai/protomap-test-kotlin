package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial ledgers by code. */
interface AccessorialLedgerStore {
    fun find(code: String): AccessorialLedger?
    fun save(item: AccessorialLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialLedgerStore(
    var items: MutableMap<String, AccessorialLedger>,
    var order: MutableList<String>,
) : AccessorialLedgerStore {
    override fun find(code: String): AccessorialLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialLedger) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<AccessorialLedger> {
        val out = mutableListOf<AccessorialLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialLedgerStore(): AccessorialLedgerStore {
    return MemoryAccessorialLedgerStore(mutableMapOf<String, AccessorialLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialLedgerStore(
    var inner: AccessorialLedgerStore,
    var cache: TtlCache<AccessorialLedger>,
    var hits: Int,
    var misses: Int,
) : AccessorialLedgerStore {
    override fun find(code: String): AccessorialLedger? {
        val hit = (cache.get(code) as AccessorialLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialLedger> = inner.list()
}

fun newCachedAccessorialLedgerStore(inner: AccessorialLedgerStore, ttl: Int, clock: Clock): AccessorialLedgerStore {
    return CachedAccessorialLedgerStore(
        inner = inner,
        cache = newTtlCache<AccessorialLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial ledgers. Empty fields match all. */
data class AccessorialLedgerQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialLedger): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialLedger>): MutableList<AccessorialLedger> {
        val matched = filter(items) { candidate: AccessorialLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialLedgers(store: AccessorialLedgerStore, query: AccessorialLedgerQuery): MutableList<AccessorialLedger> {
    return query.apply(store.list())
}
