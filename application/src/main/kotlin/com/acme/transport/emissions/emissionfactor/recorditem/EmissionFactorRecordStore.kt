package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor records by code. */
interface EmissionFactorRecordStore {
    fun find(code: String): EmissionFactorRecord?
    fun save(item: EmissionFactorRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorRecordStore(
    var items: MutableMap<String, EmissionFactorRecord>,
    var order: MutableList<String>,
) : EmissionFactorRecordStore {
    override fun find(code: String): EmissionFactorRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorRecord) {
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

    override fun list(): MutableList<EmissionFactorRecord> {
        val out = mutableListOf<EmissionFactorRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorRecordStore(): EmissionFactorRecordStore {
    return MemoryEmissionFactorRecordStore(
        mutableMapOf<String, EmissionFactorRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorRecordStore(
    var inner: EmissionFactorRecordStore,
    var cache: TtlCache<EmissionFactorRecord>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorRecordStore {
    override fun find(code: String): EmissionFactorRecord? {
        val hit = (cache.get(code) as EmissionFactorRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorRecord> = inner.list()
}

fun newCachedEmissionFactorRecordStore(inner: EmissionFactorRecordStore, ttl: Int, clock: Clock): EmissionFactorRecordStore {
    return CachedEmissionFactorRecordStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor records. Empty fields match all. */
data class EmissionFactorRecordQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorRecord>): MutableList<EmissionFactorRecord> {
        val matched = filter(items) { candidate: EmissionFactorRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorRecords(store: EmissionFactorRecordStore, query: EmissionFactorRecordQuery): MutableList<EmissionFactorRecord> {
    return query.apply(store.list())
}
