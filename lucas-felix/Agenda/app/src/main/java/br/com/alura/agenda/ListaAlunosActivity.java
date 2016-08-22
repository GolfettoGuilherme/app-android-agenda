package br.com.alura.agenda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import br.com.alura.agenda.adapter.AlunosAdapter;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;

public class ListaAlunosActivity extends AppCompatActivity {

    public static final int CODIGO_SMS = 321;
    private ListView listaAlunos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        //SOLICITA A PERMISSÃO DO USUARIO PARA RECEBER NOTIFICAÇÕES DE SMS DO CELULAR
        if(ContextCompat.checkSelfPermission(ListaAlunosActivity.this,Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            //se ele não tem, solicito a permissão dele para usar o recurso de MENSAGENS DO ANDROID
            ActivityCompat.requestPermissions(ListaAlunosActivity.this,new String[]{Manifest.permission.RECEIVE_SMS},15);
        }

        listaAlunos = (ListView) findViewById(R.id.lista_alunos);
        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);

                Intent intentVaiPtoFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentVaiPtoFormulario.putExtra("aluno", aluno);
                startActivity(intentVaiPtoFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(vaiProFormulario);
            }
        });

        registerForContextMenu(listaAlunos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position); // final por causa das classes anomimas

        //executado para verificar a permissão do usuario para usar o telefone na app
        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(ActivityCompat.checkSelfPermission(ListaAlunosActivity.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                //se ele não tem, solicito a permissão dele para usar o recurso de telefone do android.
                ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},123);//123 é action code, ver mais abaixo sobre
                } else{
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }

                return false;
            }
        });

        //Menu de contexto para enviar SMS para o número do aluno na agenda
        MenuItem itemSms = menu.add("Enviar SMS");
        Intent intentSms = new Intent(Intent.ACTION_VIEW);
        //envia para a intent o numero do telefone do usuario e qual portocolo de recurso será usado(sms)
        intentSms.setData(Uri.parse("sms:"+ aluno.getTelefone()));
        itemSms.setIntent(intentSms);

        //menu de contexto para abrir no google maps o endereço do aluno
        MenuItem itemMapa = menu.add("Visualizar no mapa");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        //protocolo geo que envia por GET para o geofences da Google o endereço para retornar as latitudes e lontitudes.
        intentMapa.setData(Uri.parse("geo:0,0?q="+aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        //menu de contexto  para acessar o site que o aluno setou na agenda
        String site = aluno.getSite();
        if(!site.startsWith("http://")){
            site = "http://" + site;
        }
        //caso o site não tenha o protocolo antes ^
        MenuItem itemSite = menu.add("Visitar Site");
        //Intent genérica para o app relativo ao protocolo utilizado (o Android vai dizer para o usuario qual app ele quer usar)
        Intent intentSite = new Intent(Intent.ACTION_VIEW);
        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);

        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.deleta(aluno);
                dao.close();

                carregaLista();
                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //depois de receber a permissão do usuario, sempre cai nesse método, onde eu posso fazer algo a mais.

        //e aquele actioncode pode ser usado aqui para verificar qual permissão foi chamada aqui.
        /*
        if(requestCode == 123 )
            //fazer alguma coisa
        */
    }
}
