package mobi.sevenwinds.app.author

import mobi.sevenwinds.dto.AuthorRecord
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val fio = varchar("fio", 50)
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio

    fun toResponse(): AuthorRecord {
        return AuthorRecord(id.value, fio)
    }
}