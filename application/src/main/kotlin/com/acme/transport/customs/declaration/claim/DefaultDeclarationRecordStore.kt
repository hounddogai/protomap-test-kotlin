package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default declaration records by code. */
interface DefaultDeclarationRecordStore {
    fun find(code: String): DefaultDeclarationRecord?
    fun save(item: DefaultDeclarationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDeclarationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDeclarationRecordStore(
    var items: MutableMap<String, DefaultDeclarationRecord>,
    var order: MutableList<String>,
) : DefaultDeclarationRecordStore {
    override fun find(code: String): DefaultDeclarationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDeclarationRecord) {
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

    override fun list(): MutableList<DefaultDeclarationRecord> {
        val out = mutableListOf<DefaultDeclarationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDeclarationRecordStore(): DefaultDeclarationRecordStore {
    return MemoryDefaultDeclarationRecordStore(
        mutableMapOf<String, DefaultDeclarationRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDeclarationRecordStore(
    var inner: DefaultDeclarationRecordStore,
    var cache: TtlCache<DefaultDeclarationRecord>,
    var hits: Int,
    var misses: Int,
) : DefaultDeclarationRecordStore {
    override fun find(code: String): DefaultDeclarationRecord? {
        val hit = (cache.get(code) as DefaultDeclarationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDeclarationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDeclarationRecord> = inner.list()
}

fun newCachedDefaultDeclarationRecordStore(inner: DefaultDeclarationRecordStore, ttl: Int, clock: Clock): DefaultDeclarationRecordStore {
    return CachedDefaultDeclarationRecordStore(
        inner = inner,
        cache = newTtlCache<DefaultDeclarationRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default declaration records. Empty fields match all. */
data class DefaultDeclarationRecordQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDeclarationRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefaultDeclarationRecord>): MutableList<DefaultDeclarationRecord> {
        val matched = filter(items) { candidate: DefaultDeclarationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDeclarationRecords(store: DefaultDeclarationRecordStore, query: DefaultDeclarationRecordQuery): MutableList<DefaultDeclarationRecord> {
    return query.apply(store.list())
}
