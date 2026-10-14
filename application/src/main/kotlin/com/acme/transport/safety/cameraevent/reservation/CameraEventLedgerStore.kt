package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event ledgers by code. */
interface CameraEventLedgerStore {
    fun find(code: String): CameraEventLedger?
    fun save(item: CameraEventLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventLedgerStore(
    var items: MutableMap<String, CameraEventLedger>,
    var order: MutableList<String>,
) : CameraEventLedgerStore {
    override fun find(code: String): CameraEventLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventLedger) {
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

    override fun list(): MutableList<CameraEventLedger> {
        val out = mutableListOf<CameraEventLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventLedgerStore(): CameraEventLedgerStore {
    return MemoryCameraEventLedgerStore(mutableMapOf<String, CameraEventLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventLedgerStore(
    var inner: CameraEventLedgerStore,
    var cache: TtlCache<CameraEventLedger>,
    var hits: Int,
    var misses: Int,
) : CameraEventLedgerStore {
    override fun find(code: String): CameraEventLedger? {
        val hit = (cache.get(code) as CameraEventLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventLedger> = inner.list()
}

fun newCachedCameraEventLedgerStore(inner: CameraEventLedgerStore, ttl: Int, clock: Clock): CameraEventLedgerStore {
    return CachedCameraEventLedgerStore(
        inner = inner,
        cache = newTtlCache<CameraEventLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event ledgers. Empty fields match all. */
data class CameraEventLedgerQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventLedger>): MutableList<CameraEventLedger> {
        val matched = filter(items) { candidate: CameraEventLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventLedgers(store: CameraEventLedgerStore, query: CameraEventLedgerQuery): MutableList<CameraEventLedger> {
    return query.apply(store.list())
}
