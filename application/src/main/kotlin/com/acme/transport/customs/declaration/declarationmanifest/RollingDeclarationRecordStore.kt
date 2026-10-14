package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling declaration records by code. */
interface RollingDeclarationRecordStore {
    fun find(code: String): RollingDeclarationRecord?
    fun save(item: RollingDeclarationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDeclarationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDeclarationRecordStore(
    var items: MutableMap<String, RollingDeclarationRecord>,
    var order: MutableList<String>,
) : RollingDeclarationRecordStore {
    override fun find(code: String): RollingDeclarationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDeclarationRecord) {
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

    override fun list(): MutableList<RollingDeclarationRecord> {
        val out = mutableListOf<RollingDeclarationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDeclarationRecordStore(): RollingDeclarationRecordStore {
    return MemoryRollingDeclarationRecordStore(
        mutableMapOf<String, RollingDeclarationRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDeclarationRecordStore(
    var inner: RollingDeclarationRecordStore,
    var cache: TtlCache<RollingDeclarationRecord>,
    var hits: Int,
    var misses: Int,
) : RollingDeclarationRecordStore {
    override fun find(code: String): RollingDeclarationRecord? {
        val hit = (cache.get(code) as RollingDeclarationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDeclarationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDeclarationRecord> = inner.list()
}

fun newCachedRollingDeclarationRecordStore(inner: RollingDeclarationRecordStore, ttl: Int, clock: Clock): RollingDeclarationRecordStore {
    return CachedRollingDeclarationRecordStore(
        inner = inner,
        cache = newTtlCache<RollingDeclarationRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling declaration records. Empty fields match all. */
data class RollingDeclarationRecordQuery(
    var source: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDeclarationRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDeclarationRecord>): MutableList<RollingDeclarationRecord> {
        val matched = filter(items) { candidate: RollingDeclarationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDeclarationRecords(store: RollingDeclarationRecordStore, query: RollingDeclarationRecordQuery): MutableList<RollingDeclarationRecord> {
    return query.apply(store.list())
}
