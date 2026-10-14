package com.acme.transport.customs.declaration.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration records by code. */
interface DeclarationRecordStore {
    fun find(code: String): DeclarationRecord?
    fun save(item: DeclarationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationRecordStore(
    var items: MutableMap<String, DeclarationRecord>,
    var order: MutableList<String>,
) : DeclarationRecordStore {
    override fun find(code: String): DeclarationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationRecord) {
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

    override fun list(): MutableList<DeclarationRecord> {
        val out = mutableListOf<DeclarationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationRecordStore(): DeclarationRecordStore {
    return MemoryDeclarationRecordStore(mutableMapOf<String, DeclarationRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationRecordStore(
    var inner: DeclarationRecordStore,
    var cache: TtlCache<DeclarationRecord>,
    var hits: Int,
    var misses: Int,
) : DeclarationRecordStore {
    override fun find(code: String): DeclarationRecord? {
        val hit = (cache.get(code) as DeclarationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationRecord> = inner.list()
}

fun newCachedDeclarationRecordStore(inner: DeclarationRecordStore, ttl: Int, clock: Clock): DeclarationRecordStore {
    return CachedDeclarationRecordStore(
        inner = inner,
        cache = newTtlCache<DeclarationRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration records. Empty fields match all. */
data class DeclarationRecordQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationRecord>): MutableList<DeclarationRecord> {
        val matched = filter(items) { candidate: DeclarationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationRecords(store: DeclarationRecordStore, query: DeclarationRecordQuery): MutableList<DeclarationRecord> {
    return query.apply(store.list())
}
